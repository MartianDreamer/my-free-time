# my-free-time

A better APIs (with a simple stupid ui) for time management tool.<br>
Put mytime web service url to environment variable "mytime_ws_url".<br>
If you're absent in the morning it will check in at 13:30. If you're absent in the afternoon, it will check out at 12:00.<br>
It can recognize correctly April 30th, May 1st as public holiday.<br>
But it may have some problem with September 2nd. 
Because one of both September 1st and September 3rd could be a public holiday and how to make the decision is unknown.
So I decided if September 1st is Monday or Thursday it will be the public holiday. If September 3rd is Tuesday or Friday it will be public holiday.
If they are not these 4 day of week, you will have to add absent day manually.<br>
It requires jre 21.<br>
Default port is 22112.

Goto http://localhost:22112 for a stupid simple ui.