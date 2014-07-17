#!/usr/bin/python
import os
import sys
import smtplib
import glob
from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email import Encoders

deviceType = ""
event = ""
identifier = ""

if len(sys.argv) >= 3:
	deviceType = sys.argv[1]
	identifier = sys.argv[2]
	event = sys.argv[3]

try:
	f = open("/private/tmp/" + deviceType + "-" + identifier + "-" + event + ".txt", "w")
except IOError:
	pass

msg = MIMEMultipart()

msg['From'] = "sender@gmail.com"
msg['To'] = "recipient@company.com"
msg['Subject'] = deviceType + " " + event

msg.attach(MIMEText(event + " event occurred for " + deviceType + " with identifier: " + identifier))

mailServer = smtplib.SMTP("smtp.gmail.com", 587)
mailServer.ehlo()
mailServer.starttls()
mailServer.ehlo()
mailServer.login("sender@gmail.com", "password")
mailServer.sendmail("sender@gmail.com", "recipient@company.com", msg.as_string())
# Should be mailServer.quit(), but that crashes...
mailServer.close()
