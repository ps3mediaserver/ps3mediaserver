#!/bin/bash

# Switch to the application directory
cd "$(dirname "$0")"

java -cp lib/*:pms.jar net.pms.PMS
