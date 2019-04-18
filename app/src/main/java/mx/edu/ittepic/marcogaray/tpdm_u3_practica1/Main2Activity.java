package mx.edu.ittepic.marcogaray.tpdm_u3_practica1;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class Main2Activity extends AppCompatActivity {
    EditText nombre, domicilio, edad, telefono;
    Button insertar, eliminar, consultar;
    FirebaseFirestore servicioBaseDatos;
    ListView listado;
    //--
    String con;
    List<Alumno> datosAlumnos;
    List<String> ramas;
    List<String> items;
    ArrayAdapter adp;
    Map<String, Object> datos;
    //--
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        nombre = findViewById(R.id.nombrea);
        domicilio = findViewById(R.id.domicilioa);
        telefono = findViewById(R.id.telefonoa);
        insertar = findViewById(R.id.insertara);
        eliminar = findViewById(R.id.eliminara);
        consultar = findViewById(R.id.consultara);
        listado = findViewById(R.id.listaa);
        con = "";
        servicioBaseDatos = FirebaseFirestore.getInstance();

        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarAlumnoAutoID();
                //insertarAlumnoTelefonoID();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarAlumno();
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultarTodos();
                //consultarPorTelefono();
            }
        });

    }

    private void insertarAlumnoAutoID(){
        Alumno alu = new Alumno(nombre.getText().toString(), domicilio.getText().toString(), edad.getText().toString(),telefono.getText().toString());

        servicioBaseDatos.collection("alumnos") //como organizarlos
                .add(alu)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Se pudo
                        Toast.makeText(Main2Activity.this, "Se insertó", Toast.LENGTH_SHORT).show();
                        nombre.setText("");
                        domicilio.setText("");
                        edad.setText("");
                        telefono.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //No se pudo
                        Toast.makeText(Main2Activity.this, "Ocurrió un error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void insertarAlumnoTelefonoID(){
        Alumno alu = new Alumno(nombre.getText().toString(), domicilio.getText().toString(), edad.getText().toString(), telefono.getText().toString());

        servicioBaseDatos.collection("alumnos")
                .document(telefono.getText().toString())
                .set(alu)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main2Activity.this, "Se insertó correctamente", Toast.LENGTH_SHORT).show();
                        nombre.setText("");
                        domicilio.setText("");
                        telefono.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Main2Activity.this, "Error al insertar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarAlumno(){
        AlertDialog.Builder a = new AlertDialog.Builder(this);
        final EditText idEliminar = new EditText(this);
        idEliminar.setHint("No debe quedar vacio!");

        a.setTitle("Atención!").setMessage("Ingrese el id del alumno a eliminar")
                .setView(idEliminar)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(idEliminar.getText().toString().isEmpty()){
                            Toast.makeText(Main2Activity.this, "Ingrese el id por favor", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminarAlumno2(idEliminar.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarAlumno2(String idEliminar){
        servicioBaseDatos.collection("alumnos")
                .document(idEliminar)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main2Activity.this, "Se logró eliminarlo", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Main2Activity.this, "No se encontró coincidencia!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void consultarTodos(){
        items = new ArrayList<>();
        adp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, items);
        listado.setAdapter(adp);
        servicioBaseDatos.collection("alumnos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        con = "";
                        datosAlumnos = new ArrayList<>();
                        ramas = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot registro : task.getResult()){
                                Map<String, Object> datos = registro.getData();

                                ramas.add(registro.getId()); //Se guarda el id de este registro
                                Alumno al = new Alumno(datos.get("nombre").toString(), datos.get("domicilio").toString(), datos.get("edad").toString(), datos.get("telefono").toString());
                                datosAlumnos.add(al); //Agregando al alumno
                            }
                            ponerloEnListView();
                        }else {
                            Toast.makeText(Main2Activity.this, "Error al consultar, \nNo hay datos a mostrar!", Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(Main2Activity.this, ""+con, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void ponerloEnListView(){
        if(datosAlumnos.size() == 0){
            return;
        }
        String[] datos = new String[datosAlumnos.size()];
        for(int i = 0; i<datos.length; i++){
            Alumno al = datosAlumnos.get(i);
            datos[i] = al.nombre+"\n"+al.telefono;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, datos);
        listado.setAdapter(adapter);

        listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder a = new AlertDialog.Builder(Main2Activity.this);
                final EditText ide = new EditText(Main2Activity.this);
                ide.setText(ramas.get(position)); //Aqui se pega el id al campo texto
                a.setTitle("Atención").setMessage("Este es el id del registro: ")
                        .setView(ide)
                        .setPositiveButton("Ok", null)
                        .show();
            }
        });
    }

    private void consultarPorTelefono(String telefonoABuscar){
        servicioBaseDatos.collection("alumnos")
                .whereEqualTo("telefono", telefonoABuscar)
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
                                        edad.setText(dato.get("edad").toString());
                                        telefono.setText(dato.get("telefono").toString());
                                    }
                                }else{
                                    String error = ""; //e.getMessage();
                                    Toast.makeText(Main2Activity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
    }

}
