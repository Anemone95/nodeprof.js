# Makefile for NodeProf.js build
# This ensures the correct Python version is used for Node.js configure

# Path to mx build tool
MX := ../mx/mx

# Python 3.11 location
PYTHON3_11 := /opt/homebrew/bin/python3.11

# Create a symlink to python3.11 in /tmp and add to PATH
PYTHON_SYMLINK := /tmp/python3
export PATH := /tmp:$(PATH)

# Java home
export JAVA_HOME := /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home

.PHONY: all configure build sforceimports clean

all: configure build

# Setup Python symlink
setup-python:
	@echo "Setting up Python 3.11 symlink..."
	@rm -f $(PYTHON_SYMLINK)
	@ln -sf $(PYTHON3_11) $(PYTHON_SYMLINK)
	@echo "Python symlink created: $(PYTHON_SYMLINK) -> $(PYTHON3_11)"

# Configure Node.js with Python 3.11
configure: setup-python
	@echo "Configuring GraalJS Node.js..."
	cd ../graaljs/graal-nodejs && \
	rm -f configure.pyc && \
	$(PYTHON3_11) configure \
		--partly-static \
		--without-dtrace \
		--without-inspector \
		--without-node-snapshot \
		--without-node-code-cache \
		--java-home $(JAVA_HOME)

# Force reimport of dependencies
sforceimports: setup-python
	@echo "Forcing reimport of dependencies..."
	$(MX) sforceimports

# Build NodeProf
build: setup-python
	@echo "Building NodeProf..."
	$(MX) build

# Full rebuild: sforceimports -> configure -> build
rebuild: sforceimports configure build

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	$(MX) clean
	rm -f $(PYTHON_SYMLINK)
	rm -f ../graaljs/graal-nodejs/configure.pyc

# Show help
help:
	@echo "NodeProf.js Build Makefile"
	@echo ""
	@echo "Targets:"
	@echo "  all           - Configure and build (default)"
	@echo "  configure     - Configure Node.js with Python 3.11"
	@echo "  build         - Build NodeProf"
	@echo "  sforceimports - Force reimport of mx dependencies"
	@echo "  rebuild       - Full rebuild (sforceimports + configure + build)"
	@echo "  clean         - Clean build artifacts"
	@echo "  help          - Show this help message"
	@echo ""
	@echo "Environment:"
	@echo "  PYTHON3_11 = $(PYTHON3_11)"
	@echo "  JAVA_HOME  = $(JAVA_HOME)"
	@echo "  PATH       = $(PATH)"
