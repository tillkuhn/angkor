resource "aws_s3_bucket_object" "bootjar" {
  bucket = aws_s3_bucket.data.bucket
  key    = "deploy/app.jar"
  source = "${path.module}/../api/build/libs/app.jar"
  storage_class = "REDUCED_REDUNDANCY"
  etag = filemd5("${path.module}/../api/build/libs/app.jar")
}

data "archive_file" "webapp" {
  type        = "zip"
  output_path = "${path.module}/../ui/dist/webapp.zip"
  source_dir = "${path.module}/../ui/dist/webapp"
  // source_dir = "lambda/node_modules"

  //source {
  //  content  = "${data.template_file.config_json.rendered}"
  //  filename = "config.json"
  //}
}

resource "aws_s3_bucket_object" "webapp" {
  bucket = aws_s3_bucket.data.bucket
  key    = "deploy/webapp.zip"
  source = data.archive_file.webapp.output_path
  storage_class = "REDUCED_REDUNDANCY"
  etag = data.archive_file.webapp.output_md5
}
