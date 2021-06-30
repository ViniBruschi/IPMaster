package com.example.loginteste6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginteste6.Activity.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btn_logout;
    private TextView textNome;
    private DatabaseReference reference;

    Button btncalcular;
    ListView lista;
    EditText edt1, edt2;
    String mascara, endereco;
    String enderecoBroad, enderecoRede, ultimoendereco, primeiroendereco;
    String maxHosts, maxendereco, bitsrede, bitshosts;
    String[] saidas = new String[10];


    //Faz o cálculo da máscara de rede.
    public String MascaraRede(int bits){
        StringBuilder mascara = new StringBuilder();
        String mascaraOcteto;
        char[] Octeto = {'0','0','0','0','0','0','0','0'};

        for (int i = 0, b = 0; i < 4; i++) {
            for (int k = 0; k < 8; k++, b++) {
                if (b < bits)
                    Octeto[k] = '1';
            }
            mascaraOcteto = String.valueOf(Octeto);
            mascaraOcteto = String.valueOf((Integer.parseInt(mascaraOcteto, 2)));
            if(i < 3)
                mascara.append(mascaraOcteto).append(".");
            else
                mascara.append(mascaraOcteto);

            for (int c = 0; c < 8; c++)
                Octeto[c] = '0';
        }
        return mascara.toString();
    }

    //Transforma o endereco IP em binário.
    public String ConverterBinario(String[] endereco){
        String binario;
        StringBuilder binarioBuilder = new StringBuilder();
        StringBuilder enderecoBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++){

            binario = Integer.toBinaryString(Integer.parseInt(endereco[i]));
            binarioBuilder.insert(0, binario);

            while (binarioBuilder.length() < 8)
                binarioBuilder.insert(0, "0");

            binario = binarioBuilder.toString();
            enderecoBuilder.append(binario);
            binarioBuilder.setLength(0);

        }

        return enderecoBuilder.toString();

    }

    //Calcula o endereço de rede e o primeiro endereço de host.
    public String Rede(String binario, int mask, boolean host){
        int dec;
        char[] endereco = binario.toCharArray();
        char[] bits = new char[8];
        String Octeto;
        StringBuilder Rede = new StringBuilder();

        for (int i = 0, b = 0; i < 4; i++) {
            for (int k = 0; k < 8; k++, b++) {
                if (b >= mask) {
                    bits[k] = '0';
                }
                else
                    bits[k] = endereco[b];
            }

            Octeto = String.valueOf(bits);
            dec = Integer.parseInt(Octeto, 2);

            if (host && i == 3)
                dec++;

            Octeto = String.valueOf(dec);

            if(i < 3)
                Rede.append(Octeto).append(".");
            else
                Rede.append(Octeto);
        }

        return Rede.toString();

    }

    //Calcula o endereço de Broadcast e o último endereço de host.
    public String Broadcast(String binario, int mask, boolean host){
        int dec;
        char[] endereco = binario.toCharArray();
        char[] bits = new char[8];
        String Octeto;
        StringBuilder Rede = new StringBuilder();

        for (int i = 0, b = 0; i < 4; i++) {
            for (int k = 0; k < 8; k++, b++) {
                if (b >= mask)
                    bits[k] = '1';
                else
                    bits[k] = endereco[b];
            }

            Octeto = String.valueOf(bits);
            dec = Integer.parseInt(Octeto, 2);

            if (host && i == 3)
                dec--;

            Octeto = String.valueOf(dec);

            if(i < 3)
                Rede.append(Octeto).append(".");
            else
                Rede.append(Octeto);
        }

        return Rede.toString();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lista = findViewById(R.id.lista);
        mAuth = FirebaseAuth.getInstance();


        textNome = findViewById(R.id.textNome);
        btn_logout = findViewById(R.id.btn_logout);

        btncalcular = findViewById(R.id.button);
        edt1 = findViewById(R.id.editText1);
        edt2 = findViewById(R.id.editText2);

        btncalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Recebe os valores de IP e da máscara informados e os coloca nas Strings;
                String IP = edt1.getText().toString();
                String mask = edt2.getText().toString();

                if(!TextUtils.isEmpty(IP) && !TextUtils.isEmpty(mask) ) {
                    //Separa o IP em quatro strings, uma para cada octeto, e separa o valor da máscara.
                    String[] campo;
                    campo = IP.split("\\.");
                    String[] parte = mask.split("/");


                    //Calculando mascara da rede.
                    mascara = MascaraRede(Integer.parseInt(parte[1]));

                    //Conversão dos octetos em binário.
                    endereco = ConverterBinario(campo);

                    //Calculando Saídas.
                    enderecoRede = Rede(endereco, Integer.parseInt(parte[1]), false);
                    enderecoBroad = Broadcast(endereco, Integer.parseInt(parte[1]), false);
                    primeiroendereco = Rede(endereco, Integer.parseInt(parte[1]), true);
                    ultimoendereco = Broadcast(endereco, Integer.parseInt(parte[1]), true);
                    maxendereco = String.valueOf(Math.round(Math.pow(2, (32 - Integer.parseInt(parte[1])))));
                    maxHosts = String.valueOf(Integer.parseInt(maxendereco) - 2);
                    bitsrede = parte[1];
                    bitshosts = String.valueOf(32 - Integer.parseInt(parte[1]));

                    saidas[0] = "Endereço IP: " + IP;
                    saidas[1] = "Máscara de SubRede: " + mascara;
                    saidas[2] = "Endereço de Rede: " + enderecoRede;
                    saidas[3] = "Endereço de Broadcast: " + enderecoBroad;
                    saidas[4] = "Primeiro Endereço de Host: " + primeiroendereco;
                    saidas[5] = "Último Endereço de Host: " + ultimoendereco;
                    saidas[6] = "Máximo de Endereços: " + maxendereco;
                    saidas[7] = "Máximo de Hosts: " + maxHosts;
                    saidas[8] = "Bits de Rede: " + bitsrede;
                    saidas[9] = "Bits de Host: " + bitshosts;

                    List<String> Saidas = Arrays.asList(saidas);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, Saidas);
                    lista.setAdapter(adapter);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference.child("usuarios").child(mAuth.getUid()).child("ip").setValue(IP);
                    reference.child("usuarios").child(mAuth.getUid()).child("primeiroendereco").setValue(primeiroendereco);
                    reference.child("usuarios").child(mAuth.getUid()).child("ultimoendereco").setValue(ultimoendereco);
                    reference.child("usuarios").child(mAuth.getUid()).child("maxendereco").setValue(maxendereco);
                    reference.child("usuarios").child(mAuth.getUid()).child("maxHosts").setValue(maxHosts);
                    reference.child("usuarios").child(mAuth.getUid()).child("enderecoBroad").setValue(enderecoBroad);
                    reference.child("usuarios").child(mAuth.getUid()).child("mascara").setValue(mascara);
                    reference.child("usuarios").child(mAuth.getUid()).child("enderecoRede").setValue(enderecoRede);
                    reference.child("usuarios").child(mAuth.getUid()).child("bitshosts").setValue(bitshosts);
                    reference.child("usuarios").child(mAuth.getUid()).child("bitsrede").setValue(bitsrede);



                } else {
                    Toast.makeText(MainActivity.this, "Verifique se há campos não preenchidos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentuser == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            reference = FirebaseDatabase.getInstance().getReference().child("usuarios").child(mAuth.getUid());

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String data;

                        if(dataSnapshot.child("nome").exists()) {
                            data = dataSnapshot.child("nome").getValue().toString();
                            textNome.setText(data);
                        }
                        if(dataSnapshot.child("ip").exists()) {
                            data = dataSnapshot.child("ip").getValue().toString();
                            saidas[0] = "Endereço IP: " + data;
                            data = dataSnapshot.child("mascara").getValue().toString();
                            saidas[1] = "Máscara de SubRede: " + data;
                            data = dataSnapshot.child("enderecoRede").getValue().toString();
                            saidas[2] = "Endereço de Rede: " + data;
                            data = dataSnapshot.child("enderecoBroad").getValue().toString();
                            saidas[3] = "Endereço de Broadcast: " + data;
                            data = dataSnapshot.child("primeiroendereco").getValue().toString();
                            saidas[4] = "Primeiro Endereço de Host: " + data;
                            data = dataSnapshot.child("ultimoendereco").getValue().toString();
                            saidas[5] = "Último Endereço de Host: " + data;
                            data = dataSnapshot.child("maxendereco").getValue().toString();
                            saidas[6] = "Máximo de Endereços: " + data;
                            data = dataSnapshot.child("maxHosts").getValue().toString();
                            saidas[7] = "Máximo de Hosts: " + data;
                            data = dataSnapshot.child("bitsrede").getValue().toString();
                            saidas[8] = "Bits de Rede: " + data;
                            data = dataSnapshot.child("bitshosts").getValue().toString();
                            saidas[9] = "Bits de Host: " + data;

                            List<String> Saidas = Arrays.asList(saidas);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, Saidas);
                            lista.setAdapter(adapter);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("TAG", "onCancelled", databaseError.toException());
                }
            });
        }
    }

}