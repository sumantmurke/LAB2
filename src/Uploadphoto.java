
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;



import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

public class Uploadphoto  {
	public static void main(String args[]) throws IOException, URISyntaxException  {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter Email ID of user : ");
		String username = scanner.nextLine();
		System.out.println("Enter password : ");
		String password =scanner.nextLine();
		String files = null;

		System.out.println("Please Enter path to upload photos ");
		String path = scanner.nextLine();
		path = path.replace("\\", "/");

		RemoteApiOptions options = new RemoteApiOptions().server(
				"1-2.ardent-course-434.appspot.com", 443).credentials(username,
						password);
		RemoteApiInstaller installer = new RemoteApiInstaller();
		installer.install(options);
		try {
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName("sumantphotofeed");
			String uploadURL = blobstoreService.createUploadUrl("/upload", uploadOptions);
			DatastoreService datastore = DatastoreServiceFactory
					.getDatastoreService();

			//File to upload to blobstore
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			Entity item = null;
			BlobKey blobKey = null;


			//POST the request to upload data to blobstore.
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {   
					CloseableHttpClient httpclient = HttpClients.createDefault();
					try {



						HttpPost httppost = new HttpPost(uploadURL);

						FileBody bin = new FileBody(listOfFiles[i], ContentType.create("image/jpeg"), listOfFiles[i].getName());

						HttpEntity reqEntity = MultipartEntityBuilder.create()
								.addPart("photo", bin)
								.build();

						item = new Entity("Photo", listOfFiles[i].getName());
						blobKey = blobstoreService.createGsBlobKey("/gs/"
								+ "mycloudbucket" + "/" + listOfFiles[i].getName());
						item.setProperty("active", true);
						item.setProperty("blobKey", blobKey);
						item.setProperty("owner", "sumantmurke");
						item.setProperty("ownerId", "114865826169667428243");
						item.setProperty("shared", true);
						System.out.println("Please enter comment for "+ listOfFiles[i].getName());
						String comment = scanner.nextLine();
						item.setProperty("title", comment);


						httppost.setEntity(reqEntity);

						CloseableHttpResponse response = httpclient.execute(httppost);
						System.out.println("Bulk Uploading of photos started ");
						try {
							datastore.put(item);
							System.out.println("executing request " + httppost.getRequestLine());
							HttpEntity resEntity = response.getEntity();
							System.out.println("response status line "+response.getStatusLine());
							EntityUtils.consume(resEntity);
							System.out.println("Content type "
									+ resEntity.getContentType());
							System.out.println("Successfully uploaded image " + listOfFiles[i].getName() +"with comment!");

						}           
						finally {
							response.close();
						}
					}

					finally {

						httpclient.close();
					}
				}
			}
		}
		finally {
			scanner.close();
			installer.uninstall();
		}
		System.out.println("All Photos have been successfully uploaded !!");
	}

}