package worker

type Config struct {
	AwsRegion     string `default:"eu-central-1" split_words:"true" required:"true" desc:"AWS Region"`
	QueueName     string `default:"angkor-events" split_words:"true" required:"true" desc:"Name of the SQS Queue"`
	QueueURL      string `desc"AWS Queue URL, can be derived from QueueName"`
	WaitSeconds   int64  `default:"20" split_words:"true" required:"true" desc:"Seconds to wait for messages"`
	SleepSeconds  int64  `default:"40" split_words:"true" required:"true" desc:"Seconds to sleep between runs"`
	MaxMessages   int64  `default:"10" split_words:"true" required:"true" desc:"Max number of messages to fetch"`
	Delegate      string `required:"true" desc:"Script or program to delegate actions"`
	RestartAction string `default:"deploy-tools" required:"true" desc:"Action which triggers a restart for the service"`
	KafkaSupport  bool   `default:"true" desc:"Send important events to Kafka Topic(s)" split_words:"true"`
}
