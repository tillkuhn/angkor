package topkapi

import "testing"

func TestConfig(t *testing.T) {
	client := &XDGSCRAMClient{HashGeneratorFcn: SHA256}
	if client == nil {
		t.Error("nix is done")
	}
}
