package main

func mailTemplate() string {
	return `
<html>
<head>
</head>
<body>
	<h3>🤖 Your daily Remindabot Report</h3>
	<img src="https://cdn2.iconfinder.com/data/icons/date-and-time-fill-outline/64/alarm_clock_time_reminder-64.png" />
	<table cellspacing="5px" cellpadding="0" style="border: none">
		{{range .Notes}}
		<tr>
			<td>☑️ <a href="{{.NoteUrl}}" target="_note">{{.Summary}}</a></td>
			<td>⏰ due {{.DueDateHuman}}</td>
			<td>👤 {{.UserShortName}}</td>
		</tr>{{end}}
	</table>
	<footer><p style="color: #63696b; font-size: 0.875em;">
		{{.Footer}}
	</p></footer>
</body>
</html>
`
}
