package com.servlet;

import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="AlbumServlet", urlPatterns={"/AlbumStore/albums/*", "/AlbumStore/albums"})
public class AlbumServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("albumID", "fixedAlbumKey");
        responseJson.addProperty("imageSize", "1024");

        resp.setContentType("application/json");
        resp.getWriter().write(responseJson.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject albumInfo = new JsonObject();
        albumInfo.addProperty("artist", "Sex Pistols");
        albumInfo.addProperty("title", "Never Mind The Bollocks!");
        albumInfo.addProperty("year", "1977");

        resp.setContentType("application/json");
        resp.getWriter().write(albumInfo.toString());
    }
}
