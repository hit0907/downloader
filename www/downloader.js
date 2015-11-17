window.Download = function(url){

	cordova.exec(
	            function(){
	            	console.log('complete');
	            }, // success callback function
	            function(){
	            	
	            }, // error callback function
	            'Downloader', // mapped to our native Java class called "Downloader"
	            'download', // with this action name
	            [url]
	        ); 
	
}
