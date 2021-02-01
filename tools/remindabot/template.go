package main

func Mailtemplate() string {
	return `
<html>
<head>
</head>
<body>
	<h3>🤖 Your daily Remindabot Report</h3>
	<img src="https://cdn2.iconfinder.com/data/icons/date-and-time-fill-outline/64/alarm_clock_time_reminder-64.png" />
	<table cellspacing="5px" cellpadding="0" style="border: none">
		{{range .}}
		<tr>
			<td>☑️ <span title="{{.ID}}">{{.Summary}}</span></td>
			<td>⏰ due {{.DueDateHuman}}</td>
			<td>👤 {{.UserShortName}}</td>
		</tr>{{end}}
	</table>
	<small style="color: gray">&#169; 2021 · Powered by Remindabot</small>
</body>
</html>
`
}
