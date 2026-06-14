package sn.gestionimmobiliere.backend.identity.application;
import java.util.*;
public final class TenantContext{private static final ThreadLocal<UUID> CURRENT=new ThreadLocal<>();private TenantContext(){}public static void set(UUID id){CURRENT.set(id);}public static Optional<UUID> currentOrganization(){return Optional.ofNullable(CURRENT.get());}public static void clear(){CURRENT.remove();}}
