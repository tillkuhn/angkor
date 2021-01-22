package main

import (
	"crypto/tls"
	"fmt"
	"log"
	"net/mail"
	"net/smtp"
	"time"
)

type Mail struct {
	From    mail.Address
	To      mail.Address
	Subject string
	Body    string
}

func Sendmail(mail *Mail, config Config) {
	// Setup headers
	headers := make(map[string]string)
	headers["From"] = mail.From.String()
	headers["To"] = mail.To.String()
	headers["Subject"] = mail.Subject
	headers["Date"] = time.Now().Format("Mon, 02 Jan 2006 15:04:05 -0700")
	headers["MIME-version"] = "1.0"
	headers["Content-Type"] = "text/html; charset=\"UTF-8\";"

	// Setup message
	message := ""
	for k, v := range headers {
		message += fmt.Sprintf("%s: %s\r\n", k, v)
	}
	message += "\r\n" + mail.Body

	// Connect to the SMTP SmtpServer and init TLS config
	auth := smtp.PlainAuth("", config.SmtpUser, config.SmtpPassword, config.SmtpServer)
	tlsconfig := &tls.Config{
		InsecureSkipVerify: false,
		ServerName:         config.SmtpServer,
	}

	// Here is the key, you need	 to call tls.Dial instead of smtp.Dial
	// for smtp servers running on 465 that require an ssl connection
	// from the very beginning (no starttls)
	if config.SmtpDryrun {
		fmt.Printf("%v", message)
		return
	}
	log.Printf("Sending mail to %v via %s:%d", mail.To, config.SmtpServer, config.SmtpPort)
	conn, err := tls.Dial("tcp", fmt.Sprintf("%s:%d", config.SmtpServer, config.SmtpPort), tlsconfig)
	if err != nil {
		log.Panic(err)
	}

	client, err := smtp.NewClient(conn, config.SmtpServer)
	if err != nil {
		log.Panic(err)
	}

	// Auth
	if err = client.Auth(auth); err != nil {
		log.Panic(err)
	}

	// To && From
	if err = client.Mail(mail.From.Address); err != nil {
		log.Panic(err)
	}

	if err = client.Rcpt(mail.To.Address); err != nil {
		log.Panic(err)
	}

	// Data
	w, err := client.Data()
	if err != nil {
		log.Panic(err)
	}

	_, err = w.Write([]byte(message))
	if err != nil {
		log.Panic(err)
	}

	err = w.Close()
	if err != nil {
		log.Panic(err)
	}

	if err = client.Quit(); err != nil {
		log.Panic(err)
	}

}
