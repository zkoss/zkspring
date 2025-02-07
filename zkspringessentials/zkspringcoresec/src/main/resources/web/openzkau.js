/**
 * Purpose: change ZK client engine's update URI to OPEN_AU_URL which is allowed to access without authentication in Spring Security config.
 * You can include this script in a page like login page to communicate the server in a specific open URL, and
 * still keep the default /zkau protected for the rest pages.
 * Based on version: ZK 9.x or above
 */
zk.afterMount(function() {
    //this should equal to one servlet-mapping you specified for DHtmlUpdateServlet in web.xml
    //you also need to allow anonymous access for this URL in spring security config.
    const OPEN_AU_URL = 'openzkau';
	let currentDesktop = zk.Desktop.$();
    currentDesktop.updateURI = currentDesktop.updateURI.replace('zkau', OPEN_AU_URL);
});