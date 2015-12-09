<?php

require_once('loader.php');

print "Hello";
// return json response 
$json = array();

$IMEIUser  = $_REQUEST['data1']; //stripUnwantedHtmlEscape($_POST["imei"]);
$nameUser = $_REQUEST['data2']; //stripUnwantedHtmlEscape($_POST["name"]);
$messageUser  =$_REQUEST['data3']; //stripUnwantedHtmlEscape($_POST["message"]); 

/**
 * Registering a user device in database
 * Store reg id in users table
 */

if (isset($IMEIUser) && isset($nameUser) && isset($messageUser) && $IMEIUser!="" && $nameUser!="" && $messageUser!="") 
{   
	print "Hi";
   	// Store user details in db
    $res = storeUserChat($IMEIUser, $nameUser, $messageUser);
} 
else 
{
    // user details not found
	echo "Wrong values.";
}

?>