package kilic.yunus.stores.repository;

import kilic.yunus.stores.model.domain.Store;

import java.util.List;

public interface StoreRepository {

    List<Store> findAll();

    int count();
}
