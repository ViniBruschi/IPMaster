package com.example.loginteste6.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.loginteste6.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Calculator extends AppCompatActivity {
    Button btncalcular;
    EditText edt1, edt2; //edtIP, edtmask, edtrede, edtfirst, edtbroad, edtlast;
    String mascara, endereco;
    String enderecoBroad, enderecoRede, ultimoendereco, primeiroendereco;
    String maxHosts, maxendereco, bitsrede, bitshosts;


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

    //Calcula o endereço de rede.
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

    //Calcula o endereço de Broadcast.
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
        setContentView(R.layout.activity_calculator);

        String[] resp = new String[] {
                "Resultado: "
        };

        btncalcular = findViewById(R.id.button);
        edt1 = findViewById(R.id.editText1);
        edt2 = findViewById(R.id.editText2);
        ListView lv = (ListView) findViewById(R.id.historico);



        final List<String> resp_list = new ArrayList<>(Arrays.asList(resp));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, resp_list);
        lv.setAdapter(arrayAdapter);

        

        btncalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Recebe os valores de IP e da máscara informados e os coloca nas Strings;
                String IP = edt1.getText().toString();
                String mask = edt2.getText().toString();

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
                maxendereco = String.valueOf(Math.pow(2, Integer.parseInt(parte[1])));
                maxHosts = String.valueOf(Integer.parseInt(parte[1]) - 2);
                bitsrede = parte[1];
                bitshosts = String.valueOf(32 - Integer.parseInt(parte[1]));



                resp_list.add("Endereço IP: " + IP);
                resp_list.add("Máscara de SubRede: " + mascara);
                resp_list.add("Endereço de Rede: " + enderecoRede);
                resp_list.add("Primeiro Endereço: " + primeiroendereco);
                resp_list.add("Último Endereço: " + ultimoendereco);
                resp_list.add("Endereço de Broadcast: " + enderecoBroad);
                resp_list.add("Máximo de Endereços: " + maxendereco);
                resp_list.add("Máximo de Hosts: " + maxHosts);
                resp_list.add("Bits de Rede: " + bitsrede);
                resp_list.add("Bits de Host: " + bitshosts);
                arrayAdapter.notifyDataSetChanged();


                /*edtIP.setText(IP);
                edtmask.setText(mascara);
                edtrede.setText(enderecoRede);
                edtfirst.setText(primeiroendereco);
                edtlast.setText(ultimoendereco);
                edtbroad.setText(enderecoBroad);*/

            }
        });
    }

}