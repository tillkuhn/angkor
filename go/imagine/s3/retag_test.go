package s3

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestRequireTagUpdateFalse(t *testing.T) {
	tagMap := make(map[string]string)
	tagMap["Title"] = "Sing me a sing"
	assert.Equal(t, true, requiresTagUpdate(tagMap))
	tagMap["Rating"] = "42"
	assert.Equal(t, false, requiresTagUpdate(tagMap))
}
func TestRequireTagUpdate(t *testing.T) {
	tagMap := make(map[string]string)
	tagMap["Nix"] = "Sing me a sing"
	assert.Equal(t, true, requiresTagUpdate(tagMap))
}
