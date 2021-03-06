package org.checkerframework.languageserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public abstract class BaseDownloader {
    String author;
    String repo;
    File folder;

    public BaseDownloader(String author, String repo, File folder) {
        this.author = author;
        this.repo = repo;
        this.folder = folder;
    }

    public File download() throws IOException {
        if (!exists()) {
            return doDownload();
        }
        return getDestination();
    }

    public boolean exists() throws IOException {
        return getDestination().exists();
    }

    public URL getLatestGitHubReleaseURL() throws MalformedURLException {
        return new URL("https://api.github.com/repos/" + author + "/" + repo + "/releases/latest");
    }

    public JsonElement getLatestGitHubRelease() throws IOException {
        String json = IOUtils.toString(getLatestGitHubReleaseURL(), StandardCharsets.UTF_8.name());
        return JsonParser.parseString(json);
    }

    public URL getLatestGitHubReleaseAsset() throws IOException {
        JsonElement root = getLatestGitHubRelease();
        JsonArray assets = root.getAsJsonObject().getAsJsonArray("assets");
        return new URL(assets.get(0).getAsJsonObject().get("browser_download_url").getAsString());
    }

    // returns the File object corresponding to a file if it's successful downloaded
    public File getDestination() throws IOException {
        Path dest =
                Paths.get(
                        folder.getAbsolutePath(),
                        FilenameUtils.getName(getLatestGitHubReleaseAsset().getPath()));
        return dest.toFile();
    }

    protected File doDownload() throws IOException {
        URL assetURL = getLatestGitHubReleaseAsset();
        File dest = getDestination();
        System.out.printf("Downloading from %s to %s\n", assetURL.toString(), dest.toString());
        FileUtils.copyURLToFile(assetURL, dest);
        return dest;
    }
}
