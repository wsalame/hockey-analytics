import pika

class MessagePublisher:
    def __init__(self, queueName):
        self.queue_name = queueName

    def openConnection(self, host='localhost'):
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=host))

    def closeConnection(self):
        self.connection.close()

    def queue_declare(self, queue):
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=queue)

    def publish(self, message):
        self.channel.basic_publish(exchange='',
                                   routing_key=self.queue_name,
                                   body=message)

    def start(self, host='localhost'):
        self.openConnection(host)
        self.queue_declare(self.queue_name)


class MessageSubscriber:
    def __init__(self):
        pass

    def openConnection(self, host='localhost'):
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=host))

    def closeConnection(self):
        self.connection.close()

    def start_consuming(self, callback, queueName):
        self.queueName = queueName
        self.channel = self.connection.channel()
        self.channel.basic_consume(callback,
                              queue=queueName,
                              no_ack=True)
        self.channel.start_consuming()

    def start(self, callback, queueName, host='localhost'):
        self.openConnection(host)
        self.start_consuming(callback, queueName)


def callback(ch, method, properties, body):
    print(" [x] Received %r" % body)


if __name__ == '__main__':
    print(' [*] Waiting for messages.')

    consumer = MessageSubscriber()
    consumer.start(queueName='games', callback=callback)





