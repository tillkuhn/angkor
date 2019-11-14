## bucket for artifacts
resource "aws_s3_bucket" "data" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data"
  region = var.aws_region
  tags = map("Name", "${var.appid}-data", "appid", var.appid, "managedBy", "terraform")
}

resource "local_file" "installscript" {
  content =  templatefile("${path.module}/files/cloud-init.sh", {
    certbot_mail = var.certbot_mail,
    domain_name=var.domain_name,
    bucket_name=aws_s3_bucket.data.bucket,
    appdir = "/opt/${var.appid}"
  })
  filename = "${path.module}/local/cloud-init.sh"
}

resource "local_file" "nginxconf" {
  content =  templatefile("${path.module}/files/nginx.conf", {
    domain_name=var.domain_name,
    appdir = "/opt/${var.appid}"
  })
  filename = "${path.module}/local/nginx.conf"
}

resource "aws_s3_bucket_object" "installscript" {
  depends_on = ["local_file.installscript"]
  bucket = aws_s3_bucket.data.bucket
  key    = "deploy/cloud-init.sh"
  source = local_file.installscript.filename
  storage_class = "REDUCED_REDUNDANCY"
  etag = filemd5("${path.module}/files/cloud-init.sh")
}

resource "aws_s3_bucket_object" "nginxconf" {
  depends_on = ["local_file.nginxconf"]
  bucket = aws_s3_bucket.data.bucket
  key    = "deploy/nginx.conf"
  source = local_file.nginxconf.filename
  storage_class = "REDUCED_REDUNDANCY"
  etag = filemd5( "${path.module}/files/nginx.conf" )
}

resource "aws_s3_bucket_object" "bootjar" {
  bucket = aws_s3_bucket.data.bucket
  key    = "deploy/app.jar"
  source = "${path.module}/../api/build/libs/app.jar"
  storage_class = "REDUCED_REDUNDANCY"
  etag = filemd5("${path.module}/../api/build/libs/app.jar")
}

resource "aws_s3_bucket_object" "appservice" {
  bucket = aws_s3_bucket.data.bucket
  key    = "deploy/app.service"
  source = "${path.module}/files/app.service"
  storage_class = "REDUCED_REDUNDANCY"
  etag = filemd5("${path.module}/files/app.service")
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
