package org.exoplatform.mobile;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/mobile")
@Api(tags = "/mobile", value = "/mobile")
public class MobileRestService implements ResourceContainer {

  @GET
  @Path("/app/download")
  @ApiOperation(value = "redirect the user to the appropriate link to download the mobile application", httpMethod = "GET", response = String.class, 
      notes = "Use this method as an intermediate step when scanning the QR code to download the mobile application. The method will parse the user agent provided in the header and will redirect the user to either google play or the app store.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled. user-agent pertaining to an android or an ios device", response = String.class),
      @ApiResponse(code = 400, message = "the device that requested the service is not an android or an ios") })
  public Response redirectToStore(@ApiParam(value = "the user-agent, ex: 'Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0'", required = true)
  @HeaderParam("user-agent")
  String userAgent) {

    if (QRcodeUtil.isIphone(userAgent)) {
      return Response.temporaryRedirect(URI.create(QRcodeUtil.appleDownloadUrl)).build();
    }
    if (QRcodeUtil.isAndroid(userAgent))
      return Response.temporaryRedirect(URI.create(QRcodeUtil.androidDownloadUrl)).build();
    else
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity("please use an iphone or an android to scan the qr code")
                     .build();

  }

}
