package com.example.sergio.myapplication.backend.servlet;

import com.example.sergio.myapplication.backend.Constants;
import com.example.sergio.myapplication.backend.domain.Conference;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.example.sergio.myapplication.backend.services.OfyService.ofy;

/**
 * Created by sergio on 17/05/15.
 */
public class SetAnnouncementServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {

        // Query for conferences with less than 5 seats left
        Iterable<Conference> iterable = ofy().load().type(Conference.class)
                .filter("seatsAvailable <", 5)
                .filter("seatsAvailable >", 0);

        // Iterate over the conferences with less than 5 seats less and get the name of each one
        List<String> conferenceNames = new ArrayList<>(0);
        for (Conference conference : iterable) {
            conferenceNames.add(conference.getName());
        }
        if (conferenceNames.size() > 0) {

            // Build a String that announces the nearly sold-out conferences
            StringBuilder announcementStringBuilder = new StringBuilder(
                    "Last chance to attend! The following conferences are nearly sold out: ");
            Joiner joiner = Joiner.on(", ").skipNulls();
            announcementStringBuilder.append(joiner.join(conferenceNames));

            // Get the Memcache Service
            MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
            // Put the announcement String in memcache. Keyed by Constants.MEMCACHE_ANNOUNCEMENTS_KEY
            String announcementKey = Constants.MEMCACHE_ANNOUNCEMENTS_KEY;
            String announcementText = announcementStringBuilder.toString();
            memcacheService.put(announcementKey,announcementText);

        }

        // Set the response status to 204 which means the request was successful but there's
        // no data to send back. Browser stays on the same page if the get came from the browser
        response.setStatus(204);
    }
}
