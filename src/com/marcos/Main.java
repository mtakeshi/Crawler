package com.marcos;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static String sfURL = "http://www.shopfacil.com.br";
    private static String sfURLOppuz = "http://search.oppuz.com/opz/api/search?limit=$qtd&sort=score.desc&store=shopfacil&callback=Opz.SearchPage.callback&text=%2F$prod";
    private static Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxybl.net.bradesco.com.br", 80));

    public static void main(String[] args) throws Exception {

        BufferedWriter bw = new BufferedWriter(new FileWriter("output_skus.txt"));
        List<String> lSecoes = new ArrayList<>();

        URLConnection sfConn = (new URL(sfURL)).openConnection(proxy);
        BufferedReader sfBuffer = new BufferedReader(new InputStreamReader(sfConn.getInputStream()));

        String linha;

        Boolean bFoundMenu = false;
        while ((linha = sfBuffer.readLine()) != null) {
            if (linha.indexOf("ico-tecnologia-todos") != -1) bFoundMenu = true;
            while (linha.indexOf("href") != -1 && bFoundMenu) {
                linha = linha.substring(linha.indexOf("href")+6);
                String complemento = linha.substring(0, linha.indexOf("\""));
                if (complemento.indexOf("opz") != -1)
                    lSecoes.add(complemento.substring(1, complemento.indexOf("?")));
            }
        }

        sfBuffer.close();

        System.out.println("SEÇÕES:" + lSecoes.size());

        //--------------------------------------

        for (String linkSecao : lSecoes) {
            linkSecao = sfURLOppuz.replace("$prod", linkSecao);
            try {
                sfConn = (new URL(linkSecao.replace("$qtd", "1"))).openConnection(proxy);
                sfBuffer = new BufferedReader(new InputStreamReader(sfConn.getInputStream()));

                while ((linha = sfBuffer.readLine()) != null) {
                    if (linha.indexOf("\"total\":") != -1) {
                        linha = linha.substring(linha.indexOf("\"total\":") + 8);
                        linkSecao = linkSecao.replace("$qtd", linha.substring(0, linha.indexOf(",")));
                        System.out.println(linkSecao);
                        break;
                    }
                }

                sfConn = (new URL(linkSecao)).openConnection(proxy);
                sfBuffer = new BufferedReader(new InputStreamReader(sfConn.getInputStream()));

                while ((linha = sfBuffer.readLine()) != null) {
                    while (linha.indexOf("\"sku\":") != -1) {
                        linha = linha.substring(linha.indexOf("\"sku\":") + 7);
                        String skuCod = linha.substring(0, linha.indexOf("\""));
                        bw.write(skuCod + ", ");
                    }
                }

                bw.flush();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (sfBuffer != null) {
                sfBuffer.close();
            }
        }

        bw.close();
    }
}
