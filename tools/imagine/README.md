# docs

* [Riding the wave. home Writing worker queues, in Go](https://nesv.github.io/golang/2014/02/25/worker-queues-in-go.html)
* [JOB QUEUES IN GO](https://www.opsdash.com/blog/job-queues-in-go.html)
* [Singleton pattern  in go](http://marcio.io/2015/07/singleton-pattern-in-go/)
* [A simple function to generate s3 presigned url in Go](https://gist.github.com/wingkwong/a7a33fee0b640997991753d9f06ff120)

# run
```
go mod init github.com/tillkuhn/angkor/tools/imaging
go get -u github.com/aws/aws-sdk-go/...
go get -u github.com/rwcarlsen/goexif/...
go get -u github.com/disintegration/imaging/... 
go: downloading github.com/aws/aws-sdk-go v1.35.14
go run main.go
```

# other things to do
```
// Crop the original image to 300x300px size using the center anchor.
src = imaging.CropAnchor(src, 300, 300, imaging.Center)

// Resize the cropped image to width = 200px preserving the aspect ratio.
src = imaging.Resize(src, 200, 0, imaging.Lanczos)

// Create a blurred version of the image.
img1 := imaging.Blur(src, 5)

// Create a grayscale version of the image with higher contrast and sharpness.
img2 := imaging.Grayscale(src)
img2 = imaging.AdjustContrast(img2, 20)
img2 = imaging.Sharpen(img2, 2)

// Create an inverted version of the image.
img3 := imaging.Invert(src)

// Create an embossed version of the image using a convolution filter.
img4 := imaging.Convolve3x3(
    src,
    [9]float64{
        -1, -1, 0,
        -1, 1, 1,
        0, 1, 1,
    },
    nil,
)

// Create a new image and paste the four produced images into it.
dst := imaging.New(400, 400, color.NRGBA{0, 0, 0, 0})
dst = imaging.Paste(dst, img1, image.Pt(0, 0))
dst = imaging.Paste(dst, img2, image.Pt(0, 200))
dst = imaging.Paste(dst, img3, image.Pt(200, 0))
dst = imaging.Paste(dst, img4, image.Pt(200, 200))
	
```


