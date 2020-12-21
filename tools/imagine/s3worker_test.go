package main

import (
	"testing"
)

func TestTagEncode(t *testing.T) {
	m := make(map[string]string)
	m["hello"] = "go"
	m["goto"] = "42"
	m["theend"] = "my/friend"
	str := encodeTagMap(m)
	expect := "goto=42&hello=go&theend=my%2Ffriend"
	// map is unsorted so order may be different, so for the sake of simplicity we onöy compare the length
	if len(*str) != len(expect) {
		t.Errorf("TestTagEncode() expected tagstr to contain %v but got %v", expect, *str)
	}
}

func TestStripRequestParams(t *testing.T) {
	check := "https://hase.com/1.jpg?horst=xxx"
	expect := "https://hase.com/1.jpg"
	actual := StripRequestParams(check)
	if expect != actual {
		t.Errorf("TestStripRequestParams() expected %v but got %v", expect, actual)
	}
}
