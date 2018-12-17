/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.converter;

/**
 *
 * @author gdimitrova
 */
public interface JsonConverter<T> {
    String toString(T obj);
    T toObject(String json);
}
