
from __future__ import print_function

pre = "INSERT INTO users "
queries = []
for i in range(200):
	team = "'red_team'"
	if i > 75 and i < 150:
		team = "'blue_team'"
	if i >= 150:
		team = "'green_team'"

	query = "INSERT INTO users (writeToken, readToken, displayName, teamId, score, lastActive) VALUES ({}, {}, {}, {}, {}, {})".format(
		"'writetok_" + str(i) + "'",
		"'readtok_" + str(i) + "'",
		"'testuser_" + str(i) + "'",
		team,
		0,
		1526617410000
	)

	print(query + ";")
