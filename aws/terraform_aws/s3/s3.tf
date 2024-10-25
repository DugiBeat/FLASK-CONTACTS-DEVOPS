provider "aws" {
  region = "us-east-1"
}


resource "aws_s3_bucket" "my_bucket" {
  bucket = "my-tf-test-bucket12345-shashkist"
  tags = {
    Description = "My test bucket"
  }
}

resource "aws_s3_object" "object1" {
  bucket = aws_s3_bucket.my_bucket.bucket
  key    = "my file.txt"
  source = "s3.tf"

  
}