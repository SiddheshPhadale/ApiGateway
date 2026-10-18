package com.Siddhesh.ApiGateway.Gateway.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@Component
public class UrlSecurityValidator {

    @Value("${gateway.allow-private-ips:false}")
    private boolean allowPrivateIps;

    public void validateUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("Target URL cannot be blank");
        }
        URI uri;
        try {
            uri = URI.create(urlString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only HTTP and HTTPS protocols are supported, received: " + scheme);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL must contain a valid host");
        }

        if (allowPrivateIps) {
            return;
        }

        if (host.equalsIgnoreCase("localhost") || host.endsWith(".localhost") || host.equalsIgnoreCase("metadata.google.internal")) {
            throw new IllegalArgumentException("Access to localhost/internal metadata host is blocked for security");
        }

        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (isBlockedAddress(address)) {
                    throw new IllegalArgumentException("Access to private/local/link-local IP addresses is blocked: " + address.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unable to resolve host: " + host);
        }
    }

    public boolean isBlockedAddress(InetAddress address) {
        if (allowPrivateIps) return false;
        return address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || address.isAnyLocalAddress();
    }

    public void setAllowPrivateIps(boolean allowPrivateIps) {
        this.allowPrivateIps = allowPrivateIps;
    }
}
