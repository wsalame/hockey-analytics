<!DOCTYPE html>
<html>
<head>

    <!--LOAD PRE-REQUISITES FOR GOOGLE SIGN IN -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js">
    </script>

    <script src="https://apis.google.com/js/platform.js?onload=start"></script>

    <!-- END PRE-REQUISITES FOR GOOGLE SIGN IN -->



<script>

    function isAuthorized(authResult) {
        return authResult['code'];
    }

    function signInCallback(authResult) {
        if (isAuthorized(authResult)) {
            // Hide the sign-in button now that the user is authorized
            $('#signinButton').attr('style', 'display: none');
            // Send the one-time-use code to the server, if the server responds, write a 'login successful' message to the web page and then redirect back to the main restaurants page
            $.ajax({
                type: 'POST',
                url: '/oauth?callback=${sessionId}',
                processData: false,
                data: authResult['code'],
                contentType: 'application/octet-stream; charset=utf-8',
                success: function (result) {
                    // Handle or verify the server response if necessary.
                    if (result) {
                        $('#result').html('Login Successful!</br>' + result + '</br>Redirecting...')
                        setTimeout(function () {
                            window.location.href = "/sup";
                        }, 8000);

                    } else if (authResult['error']) {
                        console.log('There was an error: ' + authResult['error']);
                    } else {
                        $('#result').html('Failed to make a server-side call. Check your configuration and console.');
                    }
                }

            });
        }
    }
</script>

</head>


<body>

<div>hi</div>

<!-- GOOGLE PLUS SIGN IN BUTTON-->


<div id="signInButton">
          <span class="g-signin"
                data-scope="openid email"
                data-clientid="${clientId}"
                data-redirecturi="postmessage"
                data-accesstype="offline"
                data-cookiepolicy="single_host_origin"
                data-callback="signInCallback"
                data-approvalprompt="force">
          </span>
</div>

<div id="result"></div>

<!--END GOOGLE PLUS SIGN IN BUTTON -->

</body>

</html>