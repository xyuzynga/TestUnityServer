/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.client;

import com.kakapo.unity.message.Message;

/**
 *
 * @author felix.vincent
 */
public abstract interface Connection {

    public abstract void receive(Message paramMessage);
}
