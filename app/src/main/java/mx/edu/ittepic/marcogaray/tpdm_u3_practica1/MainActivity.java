package mx.edu.ittepic.marcogaray.tpdm_u3_practica1;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText nombre, domicilio, rfc, telefono;
    Button insertar, eliminar, consultar, insertaralumno;
    FirebaseFirestore servicioBaseDatos;
    ListView listado;
    //-----
    String con="";
    List<Docente> datosDocentes;
    List<String> ramas;
    List<String> items;
    ArrayAdapter adp;
    Map<String, Object> datos;


    //-----


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombre = findViewById(R.id.nombre);
        domicilio = findViewById(R.id.domicilio);
        rfc = findViewById(R.id.rfc);
        telefono = findViewById(R.id.telefono);
        insertar = findViewById(R.id.insertar);
        eliminar = findViewById(R.id.eliminar);
        consultar = findViewById(R.id.consultar);
        listado = findViewById(R.id.lista);
        insertaralumno = findViewById(R.id.insertaralumno);

        servicioBaseDatos = FirebaseFirestore.getInstance();

        insertaralumno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
                finish();
            }
        });

        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarDocenteAutoID();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarDocente();
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultarTodos();
                //consultarId();
            }
        });


    }

    private void consultarTodos(){
        //-------------------------
        items = new ArrayList<>();
        adp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, items);
        listado.setAdapter(adp);
        //-------------------------
        servicioBaseDatos.collection("docentes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        con = "";
                        datosDocentes = new ArrayList<>();
                        ramas = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot registro : task.getResult()){
                                Map<String, Object> datos = registro.getData();
                                ramas.add(registro.getId()); //Se guarda el id de este registro
                                Docente doc = new Docente(datos.get("nombre").toString(), datos.get("domicilio").toString(), datos.get("rfc").toString(), datos.get("telefono").toString());
                                datosDocentes.add(doc); //Agregando al docente

                            }
                            ponerloEnListView();
                        }else {
                            Toast.makeText(MainActivity.this, "Error al consultar, \nNo hay datos a mostrar!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void ponerloEnListView(){
        if(datosDocentes.size() == 0){
            return;
        }
        String[] datos = new String[datosDocentes.size()];
        for(int i = 0; i<datos.length; i++){
            Docente doc = datosDocentes.get(i);
            datos[i] = doc.nombre+"\n"+doc.telefono;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, datos);
        listado.setAdapter(adapter);

        listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder a = new AlertDialog.Builder(MainActivity.this);
                final EditText ide = new EditText(MainActivity.this);
                ide.setText(ramas.get(position)); //Aqui se pega el id al campo texto
                a.setTitle("Atención").setMessage("Este es el id del registro: ")
                        .setView(ide)
                        .setPositiveButton("Ok", null)
                        .show();
            }
        });
    }

    private void consultarId(){
        AlertDialog.Builder a = new AlertDialog.Builder(this);
        final EditText porId = new EditText(this);
        porId.setHint("Ingrese el id");
        porId.setInputType(InputType.TYPE_CLASS_PHONE);
        a.setTitle("Búsqueda").setMessage("Ingrese el id")
                .setView(porId)
                .setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(porId.getText().toString().isEmpty()){
                            Toast.makeText(MainActivity.this, "Ingrese el id!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        consultarId(porId.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void consultarId(String idABuscar){
        servicioBaseDatos.collection("docentes")
                .whereEqualTo("rfc", idABuscar)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Query q = queryDocumentSnapshots.getQuery();

                        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot registro : task.getResult()){
                                        Map<String, Object> dato = registro.getData();
                                        nombre.setText(dato.get("nombre").toString());
                                        domicilio.setText(dato.get("domimcilio").toString());
                                        telefono.setText(dato.get("telefono").toString());
                                        rfc.setText(dato.get("rfc").toString());
                                    }
                                }else{
                                    String error = ""; //e.getMessage();
                                    Toast.makeText(MainActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
    }


    public void eliminarDocente(){
        AlertDialog.Builder a = new AlertDialog.Builder(this);
        final EditText idEliminar = new EditText(this);
        idEliminar.setHint("No debe quedar vacío");

        a.setTitle("Atención!").setMessage("Ingrese el di del docente a elminar")
                .setView(idEliminar).setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(idEliminar.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Ingresa el id!", Toast.LENGTH_SHORT).show();
                    return;
                }
                eliminarDocente2(idEliminar.getText().toString());
            }
        })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    public void eliminarDocente2(String idE){
        servicioBaseDatos.collection("docentes")
            .document(idE)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Eliminado con exito!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "No se encontró coincidencia!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void insertarDocenteAutoID(){
        Docente doc = new Docente(nombre.getText().toString(), domicilio.getText().toString(), rfc.getText().toString(), telefono.getText().toString());

        servicioBaseDatos.collection("docentes")
                .add(doc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity.this, "Se logró insertar!", Toast.LENGTH_SHORT).show();
                        nombre.setText("");
                        rfc.setText("");
                        domicilio.setText("");
                        telefono.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error al insertar!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
