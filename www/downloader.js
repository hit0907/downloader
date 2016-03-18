window.Downloader = function(opt, success, error){

	cordova.exec(
	            function(e){
	            	success && success(e);
	            }, // success callback function
	            function(e){
	            	error && error(e);
	            }, // error callback function
	            'Downloader', // mapped to our native Java class called "Downloader"
	            'download', // with this action name
	            [opt]
	        ); 
	
}
