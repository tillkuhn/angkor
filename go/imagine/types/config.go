package types

import "time"

// Config usage is displayed when called with -h
// IMAGINE_JWKS_ENDPOINT
type Config struct {
	AWSRegion     string         `default:"eu-central-1" required:"true" desc:"AWS Region"`
	ContextPath   string         `default:"" desc:"optional context path for http server e.g. /api" split_words:"false"`
	Debug         bool           `default:"false" desc:"debug mode for more verbose output"`
	Dumpdir       string         `default:"./upload" desc:"temporary local upload directory"`
	EnableAuth    bool           `default:"true" split_words:"true" desc:"Enabled basic auth checking for post and delete requests"`
	Fileparam     string         `default:"uploadfile" desc:"name of the param that holds the file in multipart request"`
	ForceGc       bool           `default:"false" split_words:"true" desc:"For systems low on memory, force gc/free memory after mem intensive ops"`
	JwksEndpoint  string         `split_words:"true" desc:"Endpoint to download JWKS"`
	KafkaSupport  bool           `default:"true" desc:"Send important events to Kafka Topic(s)" split_words:"true"`
	KafkaTopic    string         `default:"app" desc:"Default Kafka Topic for published Events" split_words:"true"`
	Port          int            `default:"8090" desc:"http server port"`
	PresignExpiry time.Duration  `default:"30m" desc:"how long presigned urls are valid"`
	QueueSize     int            `default:"10" split_words:"true" desc:"max capacity of s3 upload worker queue"`
	ResizeModes   map[string]int `default:"small:150,medium:300,large:600" split_words:"true" desc:"map modes with width"`
	ResizeQuality int            `default:"80" split_words:"true" desc:"JPEG quality for resize"`
	S3Bucket      string         `required:"true" desc:"Name of the S3 Bucket w/o s3:// prefix"`
	S3Prefix      string         `default:"imagine/" desc:"key prefix, leave empty to use bucket root"`
	Timeout       time.Duration  `default:"30s" desc:"Duration until http server times out"`
}
