# SlackNotify

**Author:** Matt Ozborn

## Overview

`SlackNotify` is a Java program designed to monitor changes on a specific region of the screen and send notifications to a Slack channel via a webhook. It allows for monitoring without the need to install additional software on company machines.

The program works by:
1. Capturing periodic screenshots of a defined area of the screen.
2. Comparing the screenshots to detect changes.
3. Sending a customizable message to a Slack channel if a change is detected.

## Features
- Monitors a specific area of the screen.
- Sends notifications to a Slack channel using a webhook URL.
- Requires no external installations, making it suitable for restricted environments.

## Usage

1. Provide the following inputs:
   - Slack webhook URL
   - Pixel coordinates for the region of the screen to monitor
   - The message to send when a change is detected
2. The program will periodically capture screenshots, detect changes, and send the message via a POST request to the Slack webhook.

## Prerequisites

- Java installed on the system.
- A valid Slack webhook URL.

## Compilation and Execution

To compile the program:
```bash
javac SlackNotify.java
```

To run the program:
```bash
java SlackNotify
```

## Disclaimer

This program is intended for environments where installing unapproved software is not feasible. Ensure that usage complies with company policies.
