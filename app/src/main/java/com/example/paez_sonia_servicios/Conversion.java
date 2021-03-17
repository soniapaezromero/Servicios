package com.example.paez_sonia_servicios;

import java.text.DecimalFormat;

public class Conversion {

        public  static String convertirEuros( String cantidad,double cambio)throws NumberFormatException{
            DecimalFormat formato= new DecimalFormat("#0.00");
            double cantEuros;
            cantEuros= Double.parseDouble(cantidad)*cambio;
            String resultado=formato.format(cantEuros);
            return resultado;
        }
        public static String convertirDolares(String cantidad, double cambio)throws NumberFormatException{
            DecimalFormat formato= new DecimalFormat("#0.00");
            double cantEuros;
            cantEuros= Double.parseDouble(cantidad)/cambio;
            String resultado=formato.format(cantEuros);
            return resultado;
        }

    }

