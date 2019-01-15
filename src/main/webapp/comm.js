/*******************************************************************************************************************

Constructor:


-loginMethod :  		Defines what should be done in case where there is no token in the storage 
						(i.e user have not logged in). User can be REDIRECTED to another page 
						(probably a login page) or a provided CALLBACK function can be called 
						(in order to prompt the user to enter credidentals or for any other reason)
						possible values : LoginMethod.Redirect, LoginMethod.Callback

-token : 				(Optional) If this parameter is defined, the value of it will be used as the token
						during connections and we will not try to find the token from the local storage. 
						In this case, loginMethod attribute is unnecessary

-noTokenCallback: 		This must be used if loginMethod attribute is set to LoginMethod.Callback,
						otherwise, it is not necessary.	It defines the callback function which will 
						be called if there is no token in the storage

-authErrorCallback: 	This defines the callback function to be called if authentication fails
						(i.e in case of a login attempt if the user enters wrong username, password 
						or if the token in the storage has been expired or invalid etc, or if we cannot connect
						to server, etc). This function must take one parameter, error information will be
						passed via this parameter

-authSuccessCallback: 	This defines the callback function to be called after we authenticate successfully
						the user (i.e after we determined the we have a valid token and we get user information
						from the server). In this callback function, you probably should make your AJAX requests
						which requires a token to load necessary data from the server, and also get user data
						by calling getUserData function.

-wsConnURL: 			(Optional) If this parameter is defined, after token validation, WebSocket connection will
						be set via "wsConnURL" address

-subscriptions: 		(Optional), after WebSocket connection will be established, this parameter will define which
						URLs, our communicator will bind to. "subscriptions" is a list of tuple. For each tuple, the
						first parameter will be the URL to bind, and the second parameter will be a callback function
						to be called when the server pushes a data through this URL. Regarding URL, if some text is
						written between !..!, it will mean that this part will be replaced by a variable. For example
						if the URL is /xyz/!username!, !username! will be replaced by real username value.

Functions:

-Login(username, 
	   password): 		Makes a login attempt by using the provided user data in the parameter

-makeRequest(data): 	Makes an AJAX request to the server. Data is a map that provides necessary parameters 
						for the AJAX request. Usable values are : 
						
						url: the URL address of the target (you should use partial URL address, for example if
						the target is http://x.y.z.a:p/api/target, then you should give "api/target" as URL, 
						because domain address will be put to the beginning automatically)

						type: Request type. Possible values: RequestMethod.GET, RequestMethod.POST

						success: (Optional) A callback function to be called when AJAX will be done successfully
						error:   (Optional) A callback function to be called if there will be an error. If not 
								 provided, the error will be handled by the defualt ErrorHandler function

-makeRequestWithFile(data) : Same with makeRequest function, but it makes the request with FormData, which allows
							 us to upload files into the server

-getUserData(): 		Returns an object which contains data (i.e name, email, etc) of the authenticated user.
						Please note that this data can be provided only after a successfull authentication occurs.
						So, if you call this function before the completion of authentication process, it will
						return null 

-redirectToLogin():     Redirects the user to the login page

*********************************************************************************************************************/

//LoginMethod enum, possible actions when there is no token
var LoginMethod = {
	"Redirect" : {},
	"Callback" : {}
};

var RequestMethod = {
	"GET" : {"name" : "GET"},
	"POST" : {"name" : "POST"}
};

function Communicator(data) {

	//Get construction parameters
	this.token 					= data["token"];
	this.loginMethod 			= data["loginMethod"];
	this.noTokenCallback 		= data["noTokenCallback"];
	this.authErrorCallback 		= data["authErrorCallback"];
	this.authSuccessCallback 	= data["authSuccessCallback"];
	this.wsConnURL				= data["wsConnURL"];
	this.subscriptions			= data["subscriptions"];

	//Public function declarations
	this.Login 					= Login;
	this.getUserData 			= getUserData;
	this.redirectToLogin 		= redirectToLogin;
	this.makeRequest 			= makeRequest;
	this.makeRequestWithFile	= makeRequestWithFile;
	this.goToSourcePage			= goToSourcePage;
	this.SignOut				= SignOut;

	//Those attributes will be populated automatically by the server
	var hostAddress 			= $("meta[property='hostAddress']").attr('url');
	var loginURL 			    = $("meta[property='loginURL']").attr('url');
	var tokenGiver				= $("meta[property='tokenGiver']").attr('url');
	var tokenValidator			= $("meta[property='tokenValidator']").attr('url');
	var indexURL    			= $("meta[property='indexURL']").attr('url');

	var socket;
	var stompClient;

	//This will contain the information about the validated user
	var userData 				= null;

	var instance				= this;

    function SignOut() {
    	localStorage.removeItem("authToken");
		window.location.href = hostAddress + "login";
    }

	//Check construction parameters against errors
	if (!this.loginMethod && !this.token)
		throw "Constructor Error : Either token or loginMethod must be defined";

	if (this.loginMethod && this.token)
		throw "Constructor Error : Both token and loginMethod should not be defined";

	if (this.loginMethod && this.loginMethod == LoginMethod.Callback && !this.noTokenCallback)
		throw "Constructor Error : When loginMethod is Callback, noTokenCallback function must be defined";

	if (this.loginMethod && this.loginMethod != LoginMethod.Redirect && this.loginMethod != LoginMethod.Callback)
		throw "Constructor Error : Unknown loginMethod is defined";

	if (this.token && this.noTokenCallback)
		throw "Constructor Error : When token is defined, no need to define noTokenCallback function"

	if (!this.authErrorCallback)
		throw "Constructor Error : authErrorCallback function must be defined";

	if (!this.authSuccessCallback)
		throw "Constructor Error : authSuccessCallback function must be defined";

	/*Now try to get the token, if token attribute is defined, we don't need to do anything,
	otherwise, we need to get the token from local storage*/
	if (!this.token)
		this.token = localStorage.getItem('authToken');

	/*Now, if we still don't have a token, it means that we need to login again in order to
	get a valid token from the server*/
	if (!this.token) {
		/*Take the necessary action according to the loginMethod value*/
		if (this.loginMethod == LoginMethod.Redirect)
    		this.redirectToLogin();
		if (this.loginMethod == LoginMethod.Callback)
			this.noTokenCallback();
		return;
	}

	/*Now, we know that we have a token, so let's validate it*/
	Validate();

    function Login(username, password) {
    	var loginData = {
    		"username" : username,
    		"password" : password
    	};

    	$.ajax({
	        url: tokenGiver,
	        type:'POST',
	        data: JSON.stringify(loginData),
	        contentType: "application/json; charset=UTF-8",
	        success:function (response) {
	        	localStorage.setItem('authToken', response.token);
                goToSourcePage()
            },
	        error:function (response) {
	        	localStorage.removeItem("authToken");
	            instance.authErrorCallback(response);
	       }
	    });
    }

    function getUserData() {
    	return userData;
    } 

    function redirectToLogin() {
        window.location.href = loginURL;
    }

    function makeRequest(data) {
    	if (!data.url)
    		throw "Invalid AJAX Request : url is not defined";

    	if (!data.type || (data.type != RequestMethod.GET && data.type != RequestMethod.POST))
    		throw "Invalid AJAX Request : type is not defined";

    	if (!data.data && data.type == RequestMethod.POST)
    		throw "Invalid AJAX Request : data must be defined when doing POST request";

    	data.url = hostAddress + data.url;

    	if (data.data && data.type == RequestMethod.GET) {
    		data.url += "?";
    		for (var key in data.data)
            	if (data.data.hasOwnProperty(key))
                	data.url += key + "=" + data.data[key] + "&";
    		data.url = data.url.substr(0, data.url.length - 1);
    	}

    	$.ajax({
    		url : data.url,
    		type: data.type.name,
    		data: (data.type == RequestMethod.POST) ? data.data : null,
    		/*this should be application/json or false. If false, no content-type header is set which is fine.
	        If we do not use this, default content-type header value is application/x-www-form-urlencoded and this
	        causes the server to append '=' at the end of json and it decodes the data as uri*/
	    	contentType: "application/json; charset=UTF-8",
	    	beforeSend: function(xhr) {
	        	xhr.setRequestHeader("Auth", instance.token);
	        },
	        success: function(response) {
	        	if (data.success)
	        		data.success(response);
	        },
	        error: function(response) {
	        	if (data.error)
	        		data.error(response);
	        	else
	        		AJAXErrorHandler(response);
	        }
    	});
    }

    function makeRequestWithFile(data) {
    	if (!data.url)
    		throw "Invalid AJAX Request : url is not defined";

    	if (!data.data)
    		throw "Invalid AJAX Request : data must be defined";

    	$.ajax({
    		url : data.url,
    		type: "POST",
    		data: data.data,
    		processData: false,
	    	contentType: false,
	    	beforeSend: function(xhr) {
	        	xhr.setRequestHeader("Auth", instance.token);
	        },
	        success: function(response) {
	        	if (data.success)
	        		data.success(response);
	        },
	        error: function(response) {
	        	if (data.error)
	        		data.error(response);
	        	else
	        		AJAXErrorHandler(response);
	        }
    	});
    }

    function Validate() {
		$.ajax({
	        url: tokenValidator,
	        type:'POST',
	        contentType: "application/json; charset=UTF-8",
	        beforeSend: function(xhr) {
	           xhr.setRequestHeader("Auth", instance.token);
	        },
	        success:function (response) {
	        	userData = response;

	        	/*Set WebSocket connection (if defined)*/
	        	if (instance.wsConnURL) {
	        		var header = {
	        			"token": instance.token
	        		};

	        		var username = instance.getUserData().username;
	        		var subscriptions = instance.subscriptions;

	        		instance.socket = new SockJS(instance.wsConnURL);
					instance.stompClient = Stomp.over(instance.socket);
					instance.stompClient.connect(header, function(frame) {
						for (var i = 0; i < subscriptions.length; i++) {
	        				subscriptions[i][0] = subscriptions[i][0].replace("!username!", username);
	        				instance.stompClient.subscribe(subscriptions[i][0], subscriptions[i][1], header);
	        			}
					});
	        	}

	            instance.authSuccessCallback();
	        },
	        error:function (response) {
	        	localStorage.removeItem("authToken");
	            instance.authErrorCallback(response);
	       }
	    });
	}

    function AJAXErrorHandler(error) {
    	if (error.status && error.statusText)
    		alert("Error: status code " + error.status + " " + error.statusText + " " + error.responseText);
    	else
    		alert(error);
    }

    function goToSourcePage() {
    	window.location.href = indexURL;
    }
}