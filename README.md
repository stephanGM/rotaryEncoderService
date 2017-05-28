# Rotary Encoder Service
A service for Android

## Overview

The Rotary Encoder Service is a simple, portable, service APK for Android that serves as an interface between a rotary encoder and the android device. The service de- tects logic level changes on GPIO pins through the sysfs interface and uses a Finite State Machine (FSM) in order to detect which direction the encoder is turning in. This direction is then broadcast as an intent so that other applications may use the input as they see fit. The broadcast will be either com.google.hal.CLOCKWISE or com.google.hal.COUNTER_CLOCKWISE.


