# Copyright 2017 Telstra Open Source
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

import functools
import json
import os
import random
import sys
import time

import datetime
import gevent
import kafka
import pytz
import uuid

from topologylistener import config
from topologylistener import eventhandler
from topologylistener import flow_utils
from topologylistener import message_utils
from topologylistener import messageclasses
from topologylistener import model

script_dir = os.path.dirname(__file__)
resource_path = functools.partial(os.path.join, script_dir, 'resources')

UNIX_EPOCH = datetime.datetime(1970, 01, 01, 0, 0, 0, 0, pytz.utc)
SCENARIO_FILE = resource_path('py2neo-self-lock-scenario.kafka')
DPID_PROTECTEd_BITS = 0xffffff0000000000
ALIVE_DATAPATH_MARKER = 0xffa00000


def main():
    random.seed()

    kafka_producer = kafka.KafkaProducer(bootstrap_servers=config.KAFKA_BOOTSTRAP_SERVERS)
    kafka_consumer = init_kafka_consumer('TE-hang-test')

    import logging
    logging.basicConfig(level=logging.INFO)

    gevent.spawn(eventhandler.main_loop)

    with open(SCENARIO_FILE, 'rt') as scenario_stream:
        loop_until_hang(kafka_producer, kafka_consumer, scenario_stream)


def loop_until_hang(producer, consumer, stream):
    iteration = 0
    while True:
        ctime = time.time()
        exec_scenario(producer, stream)
        is_alive = is_te_alive(producer, consumer)
        # drop all object in neo4j!
        #reset_db(flow_utils.graph)
        duration = time.time() - ctime

        status = 'success' if is_alive else 'hang'
        print('test iteration {:5d}: {} {}'.format(iteration, status, format_duration(duration)))

        if not is_alive:
            break

        iteration += 1


def is_te_alive(producer, consumer, timeout=5):
    endpoint = model.NetworkEndpoint(network_datapath_id(ALIVE_DATAPATH_MARKER), 512)
    timestamp = java_timestamp()
    command = kafka_command(
        payload_port_info(endpoint),
        timestamp=timestamp)
    command = pack_kafka_message(command)
    command = encode_kafka_message(command)
    producer.send(config.KAFKA_TOPO_ENG_TOPIC, command).get()

    now = time.time()
    etime = now + timeout
    match = False

    while not match and now < etime:
        for partition, batch in consumer.poll(timeout_ms=.2).items():
            if match:
                break

            for record in batch:
                decoded = decode_kafka_message(record.value)
                message = unpack_kafka_message(decoded)

                payload = message['payload']
                if payload['clazz'] != messageclasses.MT_PORT:
                    continue
                if endpoint != model.NetworkEndpoint(payload['switch_id'], payload['port_no']):
                    continue

                match = True
                break

        now = time.time()

    consumer.commit()

    return match


def reset_db(connect):
    connect.run('MATCH (a) DETACH DELETE a')


def exec_scenario(producer, stream):
    stream.seek(0, os.SEEK_SET)
    sys.stdout.write('Record ')
    try:
        for idx, record in enumerate(stream_to_records(stream)):
            sys.stdout.write('{:d} '.format(idx))
            sys.stdout.flush()

            message = encode_kafka_message(record)
            producer.send(config.KAFKA_TOPO_ENG_TOPIC, message).get()
    finally:
        print('')


def payload_port_info(endpoint, **fields):
    payload = {
        'state': 'DOWN',
        'switch_id': endpoint.dpid,
        'port_no': endpoint.port}
    payload.update(fields)
    payload['clazz'] = messageclasses.MT_PORT
    return payload


def kafka_command(payload, **fields):
    message = {
        'timestamp': 0,
        'correlation_id': command_correlation_id('test')}
    message.update(fields)
    message.update({
        'clazz': message_utils.MT_INFO,
        'payload': payload})
    return message


def command_correlation_id(prefix=''):
    if prefix and prefix[-1] != '.':
        prefix += '.'
    return '{}{}'.format(prefix, uuid.uuid1())


def java_timestamp():
    value = datetime.datetime.utcnow()
    value = value.replace(tzinfo=pytz.utc)
    from_epoch = value - UNIX_EPOCH
    seconds = int(from_epoch.total_seconds())
    return seconds * 1000 + from_epoch.microseconds // 1000


def network_datapath_id(number):
    if number & DPID_PROTECTEd_BITS:
        raise ValueError(
                'Invalid switch id {}: use protected bits'.format(number))
    return pack_dpid(number | DPID_PROTECTEd_BITS)


def pack_dpid(dpid):
    value = hex(dpid)
    i = iter(value)
    chunked = [a + b for a, b in zip(i, i)]
    chunked.pop(0)
    return ':'.join(chunked)


def stream_to_records(stream):
    for line in stream:
        yield line


def pack_kafka_message(message):
    return json.dumps(message)


def unpack_kafka_message(record):
    return json.loads(record)


def encode_kafka_message(record):
    return record.encode('utf-8')


def decode_kafka_message(record):
    return record.decode('utf-8')


def init_kafka_consumer(group):
    consumer = kafka.KafkaConsumer(
        config.KAFKA_CACHE_TOPIC,
        bootstrap_servers=config.KAFKA_BOOTSTRAP_SERVERS,
        group_id=group,
        auto_offset_reset='latest')
    consumer.assignment()
    return consumer


def format_duration(raw):
    value = int(raw)
    msec = int((raw - value) * 1000)
    seconds = value % 60
    value /= 60
    minutes = value % 60
    value /= 60
    days = value / 24

    chunks = []
    for label, data in zip('dms', [days, minutes, seconds]):
        data = int(data)
        if not data:
            continue
        chunks.append('{}{}'.format(data, label))

    if msec:
        chunks.append('.{}'.format(msec))

    return ''.join(chunks)


if __name__ == '__main__':
    sys.exit(main())
