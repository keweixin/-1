package com.bylw.service;

import com.bylw.entity.Courier;
import java.util.List;

public interface CourierService {
    List<Courier> listActive();
    List<Courier> listAll();
    Courier getById(Integer courierId);
    boolean add(Courier courier);
    boolean update(Courier courier);
    boolean delete(Integer courierId);
}
