package br.com.lopes.heider.android_downloadmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadManagerActivity extends Activity implements OnClickListener{

    private DownloadManager downloadManager;
    private long downloadReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);

        Button startDownload = (Button) findViewById(R.id.startDownload);
        startDownload.setOnClickListener(this);

        Button displayDownload = (Button) findViewById(R.id.displayDownload);
        displayDownload.setOnClickListener(this);

        Button checkStatus = (Button) findViewById(R.id.checkStatus);
        checkStatus.setOnClickListener(this);
        checkStatus.setEnabled(false);

        Button cancelDownload = (Button) findViewById(R.id.cancelDownload);
        cancelDownload.setOnClickListener(this);
        cancelDownload.setEnabled(false);

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startDownload:

                downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                Uri Download_Uri = Uri.parse("http://demo.mysamplecode.com/Sencha_Touch/CountryServlet?start=0&limit=999");
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                //Restringe os tipos de redes sobre as quais esta transferência pode prosseguir.
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                //Defina se esta transferência pode prosseguir através de uma conexão de roaming.
                request.setAllowedOverRoaming(false);
                //Defina o título deste download, a ser exibido nas notificações (se habilitado).
                request.setTitle("Download Manager");
                //Defina uma descrição deste download, a ser exibido nas notificações (se habilitado)
                request.setDescription("Download usando o Download Manager");
                //Defina o destino local para o arquivo baixado para um caminho dentro do diretório de arquivos externo do aplicativo
                request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,"CountryList.json");

                //Enfileirar um novo download e mesmo o referenceID
                downloadReference = downloadManager.enqueue(request);

                TextView showCountries = (TextView) findViewById(R.id.countryData);
                showCountries.setText("Obtendo dados do servidor, aguarde por favor...");

                Button checkStatus = (Button) findViewById(R.id.checkStatus);
                checkStatus.setEnabled(true);
                Button cancelDownload = (Button) findViewById(R.id.cancelDownload);
                cancelDownload.setEnabled(true);
                break;

            case R.id.displayDownload:

                Intent intent = new Intent();
                intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                startActivity(intent);
                break;

            case R.id.checkStatus:

                Query myDownloadQuery = new Query();
                //definir o filtro de consulta para o nosso download anteriormente enfileirado
                myDownloadQuery.setFilterById(downloadReference);

                //Consultar o gerenciador de download sobre downloads que tenham sido solicitados.
                Cursor cursor = downloadManager.query(myDownloadQuery);
                if(cursor.moveToFirst()){
                    checkStatus(cursor);
                }
                break;

            case R.id.cancelDownload:
                downloadManager.remove(downloadReference);
                checkStatus = (Button) findViewById(R.id.checkStatus);
                checkStatus.setEnabled(false);
                showCountries = (TextView) findViewById(R.id.countryData);
                showCountries.setText("Download do arquivo cancelado...");
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void checkStatus(Cursor cursor){

        //coluna com status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //coluna para o código se o download falhou ou pausada
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        //obter o nome do arquivo baixado
        int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String filename = cursor.getString(filenameIndex);

        String statusText = "";
        String reasonText = "";

        switch(status){

            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch(reason){
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;

            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch(reason){
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;

            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;

            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                reasonText = "Filename:\n" + filename;
                break;
        }


        Toast toast = Toast.makeText(DownloadManagerActivity.this,
                statusText + "\n" +
                        reasonText,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();

    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //verificar se a mensagem de difusão é para o nosso download enfileirado
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if(downloadReference == referenceId){

                Button cancelDownload = (Button) findViewById(R.id.cancelDownload);
                cancelDownload.setEnabled(false);

                int ch;
                ParcelFileDescriptor file;
                StringBuffer strContent = new StringBuffer("");
                StringBuffer countryData = new StringBuffer("");

                //analisar os dados JSON e exibir na tela
                try {
                    file = downloadManager.openDownloadedFile(downloadReference);
                    FileInputStream fileInputStream
                            = new ParcelFileDescriptor.AutoCloseInputStream(file);

                    while( (ch = fileInputStream.read()) != -1)
                        strContent.append((char)ch);

                    JSONObject responseObj = new JSONObject(strContent.toString());
                    JSONArray countriesObj = responseObj.getJSONArray("countries");

                    for (int i=0; i<countriesObj.length(); i++){
                        Gson gson = new Gson();
                        String countryInfo = countriesObj.getJSONObject(i).toString();
                        Country country = gson.fromJson(countryInfo, Country.class);
                        countryData.append(country.getCode() + ": " + country.getName() +"\n");
                    }

                    TextView showCountries = (TextView) findViewById(R.id.countryData);
                    showCountries.setText(countryData.toString());

                    Toast toast = Toast.makeText(DownloadManagerActivity.this,
                            "Download finalizado ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 25, 400);
                    toast.show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };
}