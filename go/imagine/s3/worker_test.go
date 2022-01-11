package s3

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestQueryAlreadyEscaped(t *testing.T) {
	m := make(map[string]string)
	m["hello"] = "hase+%2B+klaus+%26+rudi"
	expect := "hello=hase+%2B+klaus+%2B+rudi"
	// map is unsorted so order may be different, so for the sake of simplicity we only compare the length
	assert.Equal(t, expect, *encodeTagMap(m))
}

func TestTagEncode(t *testing.T) {
	m := make(map[string]string)
	m["hello"] = "go"
	m["goto"] = "42"
	m["theend"] = "my/friend"
	str := encodeTagMap(m)
	expect := "goto=42&hello=go&theend=my%2Ffriend"
	// map is unsorted so order may be different, so for the sake of simplicity we only compare the length
	assert.Equal(t, expect, *str)
}

func TestEncodeTagSpecialChars(t *testing.T) {
	// https://docs.aws.amazon.com/directoryservice/latest/devguide/API_Tag.html
	// https://stackoverflow.com/questions/69399496/s3-tagging-error-the-tagvalue-you-have-provided-is-invalid
	// : ^([\p{L}\p{Z}\p{N}_.:/=+\-@]*)$
	tagMap := make(map[string]string)
	tagMap["ContentType"] = "some/thing"
	tagMap["Artist"] = "Kruder & Dorfmeister"
	tagMap["Title"] = "This \" needs to be dealt with"
	tagMap["Origin"] = "https://hase.klaus.de/horst"
	tags := encodeTagMap(tagMap)
	assert.Equal(t, "Artist=Kruder+%2B+Dorfmeister&ContentType=some%2Fthing&Origin=https%3A%2F%2Fhase.klaus.de%2Fhorst&Title=This++needs+to+be+dealt+with", *tags)
}

func TestSanitizeTagValue(t *testing.T) {
	const sample = `hase & rabbit "were walking through the forest": this is the _end & that's it' https://the-end`
	assert.Equal(t, "hase + rabbit were walking through the forest: this is the _end + thats it https://the-end", sanitizeTagValue(sample))
	tooLong := strings.Repeat("01234567", 34)
	assert.Equal(t, strings.Repeat("01234567", 32), sanitizeTagValue(tooLong))
}
