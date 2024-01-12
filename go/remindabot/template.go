package main

func mailTemplate() string {
	return `
<html>
<head>
</head>
<body>
	<img src="{{ .ImageUrl }}" alt="Nice Image" />
	<h3>🤖 Your daily Remindabot Report</h3>
	<table cellspacing="5px" cellpadding="0" style="border: none">
		{{range .Notes}}
		<tr>
			<td>☑️ <a href="{{.NoteUrl}}" target="_note">{{.Summary}}</a></td>
			<td>⏰ due {{.DueDateHuman}}</td>
			<td>👤 {{.UserShortName}}</td>
		</tr>{{end}}
	</table>

	<h3>🆕 Event Digest (BETA)</h3>
	<table cellspacing="5px" cellpadding="0" style="border: none">
		<tr>
		<th>Time</th><th>Source</th><th>Event Type</th><th>Subject</th>
		</tr>
		{{range $idx, $event := .Events}}
		<tr>
			<td>{{$event.Time}}</td>
			<td>{{$event.Source}}</td>
			<td>{{$event.Type}}</td>
			<td>{{$event.Subject}}</td>
		</tr>{{end}}
	</table>

	<footer><p style="color: #63696b; font-size: 0.875em;">
		{{.Footer}}
	</p></footer>
</body>
</html>
`
}
