# Superb Warfare 0.8.9 API Changes and Migration Guide

## Breaking Changes Found:

### 1. MissileProjectile Field Encapsulation
**Old Behavior (0.8.8):**
```java
this.damage = 1100;
this.explosionDamage = 180;
this.explosionRadius = 12;
this.durability = 25;
```

**New Behavior (0.8.9):**
- These fields are now private in the parent class
- `durability` is now a final method (cannot be overridden)

**Solution:**
Use Java reflection to access and modify these private fields at runtime.

### 2. ContainerBlockItem Package Change
**Old Path:** `com.atsuishio.superbwarfare.item.common.container.ContainerBlockItem`
**New Path:** `com.atsuishio.superbwarfare.item.container.ContainerBlockItem`

### 3. Function2 Requirement
**Issue:** DamageModifier.custom() now requires Kotlin's Function2 type
**Solution:** Added Kotlin stdlib to dependencies

```groovy
// In build.gradle
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.10'
```

And in plugins section:
```groovy
id 'org.jetbrains.kotlin.jvm' version '1.9.10'
```

## Reflection-Based Field Access Pattern

The projectile classes now use this pattern to handle private fields:

```java
private static java.lang.reflect.Field damageField;

static {
    try {
        Class<?> parentClass = MissileProjectile.class;
        damageField = parentClass.getDeclaredField("damage");
        damageField.setAccessible(true);
    } catch (NoSuchFieldException e) {
        e.printStackTrace();
    }
}

private void setDamage(int value) {
    try {
        if (damageField != null) damageField.setInt(this, value);
    } catch (IllegalAccessException e) {
        e.printStackTrace();
    }
}

private int getDamage() {
    try {
        if (damageField != null) return damageField.getInt(this);
    } catch (IllegalAccessException e) {
        e.printStackTrace();
    }
    return 0;
}
```

## Other Potential Changes (To Be Tested)

1. **VehicleEntity changes** - May have new or modified methods
2. **DamageModifier API** - The custom() method signature may have changed
3. **Sound and Explosion handling** - Config structure might have changed
4. **Entity rendering** - GeckoLib compatibility needs verification

## Testing Checklist

- [ ] Compile without errors
- [ ] Launch game client
- [ ] Spawn Sidewinder missile - ensure it follows wire guidance
- [ ] Spawn Hellfire missile - ensure lock-on and wire guidance work
- [ ] Spawn Malyutka missile - ensure guidance works
- [ ] Spawn all vehicles and check for rendering/functionality issues
- [ ] Test vehicle damage modifiers
- [ ] Check creative mode tabs display correctly
- [ ] Test all weapons fire correctly

## Future Compatibility Notes

If the mod is updated further, watch for:
- More field encapsulation in base classes
- API method signature changes
- Dependency version updates (Kotlin, Forge, etc.)
- New configuration options in parent mods

Keep the reflection-based pattern for handling private fields, as this is a robust solution for API compatibility issues.

