<?php
define("LEDTEST", "/usr/bin/sudo /home/pi/ledtest");
define("ADDRESS", "127.0.0.1");
define("USER", "pi");
define("PASSWORD", "helloyamagiwashinichi-this-is-example-password");

If(!is_null($_GET["num"]) && !is_null($_GET["stat"])) {
  /* SSH2 module processes */
  $sconnection = ssh2_connect(ADDRESS, 22);
  ssh2_auth_password($sconnection, USER, PASSWORD);
  /* execution command */
  $command = LEDTEST." ".$_GET["num"]." ".$_GET["stat"];
  $stdio_stream = ssh2_exec($sconnection, $command);
}
?>
