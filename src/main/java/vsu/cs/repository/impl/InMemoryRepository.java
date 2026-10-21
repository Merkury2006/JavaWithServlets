package vsu.cs.repository.impl;

import vsu.cs.domain.Identifiable;
import vsu.cs.repository.Repository;

import java.util.*;
import java.util.function.Supplier;

public abstract class InMemoryRepository <T extends Identifiable<ID>, ID> implements Repository<T, ID> {
    protected final Map<ID, T> storage = new LinkedHashMap<>();
    private final Supplier<ID> idGenerator;

    public InMemoryRepository(Supplier<ID> idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.get());
        }
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(ID id) {
        storage.remove(id);
    }

    @Override
    public boolean existsById(ID id) {
        return storage.containsKey(id);
    }
}
