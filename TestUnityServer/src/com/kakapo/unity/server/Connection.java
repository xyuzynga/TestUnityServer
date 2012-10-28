/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.server;

import com.kakapo.unity.message.Message;

/**
 *
 * @author felix.vincent
 */
public abstract interface Connection {

    public abstract void receive(Message paramMessage);
}
