# How to run with maven
`mvn jetty:run`

visit http://localhost:8080/zkspringcoresec/


# Spring Security Use Cases 
This project plans to demonstrate the following use cases:

## login page with zul
* Users login with a form submission to avoid sending AU requests. Because /zkau is secured.
* require DHtmlResourceServlet (available since 8.6.2) to make zul page loading correctly if all URL are secured. 
* browser still can get zk resources including *.wpd, *.wcs, and zk bundled images. 

## Role-base permission control
### supervisor
* access extreme secure page
* access secure page
* change the account balance 
### teller
* access secure page
* change the account balance
### user
* access secure page


# Secure pages
* [extreme secure page](src/main/webapp/secure/extreme/index.zul) 
* [secure page](src/main/webapp/secure/index.zul)

# Operation permission control
* `org.zkoss.zkspringessentials.bigbank.BankService`

## handle zkau 302
* if you try to change the account balance without authentication, you will be redirected to login page. 
* If you change the account balance without proper authorization (e.g. with USER), you will see [ajaxDenied.zul](src/main/webapp/errors/ajaxDenied.zul)

## handle 302 when loading a zul
* if you try to access a secure page without authentication, you will be redirected to login page.
* if you try to access a secure page without proper authorization, you will see [denied.zul](src%2Fmain%2Fwebapp%2Ferrors%2Fdenied.zul)

## redirect-after-login parameter
if you visit login page with `redirect-after-login` parameter, you will be redirected to the specified page after login e.g. `/login.zul?redirect-after-login=/secure/index.zul` 