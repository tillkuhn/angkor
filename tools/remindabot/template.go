package main

func Mailtemplate() string {
	return `
<html>
<head>
</head>
<body>
	<h3>🤖 Remindabot Report</h3>
	<img src="https://cdn2.iconfinder.com/data/icons/date-and-time-fill-outline/64/alarm_clock_time_reminder-64.png" />
	<p>Hello <b>Client</b>, pls find your reminders below:</p>
	<ul>
		{{range .}}
		<li><span title="{{.ID}}">{{.Summary}}</span> due {{.DueDate}} for {{.UserShortName}}</li>
		{{end}}
	</ul>

</body>
</html>
`
}
