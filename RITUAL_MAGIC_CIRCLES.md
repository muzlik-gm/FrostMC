# Ritual Magic Circles - Rank-Based Complexity

## Overview

Ritual magic circles now scale in complexity based on fragment **base rank**, creating increasingly impressive visual displays. Each fragment type has its own base rank that determines the initial complexity of the ritual magic circle.

## Fragment Base Ranks

Each fragment has a different base rank (and can progress +2 ranks from there):

- **Water**: Base Rank 2 → Max Rank 4
- **Fire**: Base Rank 3 → Max Rank 5
- **Air**: Base Rank 3 → Max Rank 5
- **Dark**: Base Rank 4 → Max Rank 6
- **Light**: Base Rank 4 → Max Rank 6
- **Storm**: Base Rank 6 → Max Rank 8
- **Void**: Base Rank 7 → Max Rank 9
- **Dragon**: Base Rank 8 → Max Rank 10
- **Admin**: Base Rank 10 → Max Rank 12

## Rank-Based Complexity System

The ritual magic circle complexity matches the fragment's base rank, so more powerful fragments have more impressive rituals from the start!

### Rank 1-2: Basic Circles
- **Single outer circle** (3.0 block radius)
- **Inner circle** at rank 2 (2.0 block radius)
- Simple, clean design for new fragments

### Rank 3-4: Runes Appear
- **Rune circle** added (8 runes around outer edge)
- **Geometric hexagon pattern** at rank 4 (middle layer)
- Mystical symbols begin to appear

### Rank 5-6: Advanced Geometry
- **Outer circle** expands (4.0 block radius)
- **Star pattern** in center (5-pointed star)
- Multiple rotating layers create depth

### Rank 7-8: Sacred Geometry
- **Sacred geometry** layer (flower of life pattern with 12 petals)
- **Complex runes** at rank 8 (16 diamond-pattern runes)
- **Ultimate circle** (4.5 block radius)
- Maximum visual complexity with multiple rotating layers

## Visual Features

### Rotation Animation
- Each circle layer rotates at different speeds
- Creates mesmerizing, dynamic effect
- Rotation speeds vary by layer for visual interest

### Fragment-Specific Colors
Each fragment type has its own color scheme:
- **Fire**: Dark red
- **Water**: Dark blue
- **Air**: Dark gray
- **Dark**: Very dark purple
- **Light**: Dark gold
- **Void**: Almost black purple
- **Storm**: Dark blue-purple
- **Dragon**: Dark magenta

### Particle Types
- **REDSTONE** particles for circle lines (colored by fragment)
- **ENCHANTMENT_TABLE** particles for magical sparkle
- **Center glow** intensity increases with rank (2-16 particles)

## Can You Use Textures?

**Yes, but with limitations:**

### Current Particle System
The magic circles use **REDSTONE particles** with custom colors, which are:
- ✅ Highly performant
- ✅ Visible from any angle
- ✅ Easy to animate and rotate
- ✅ Support custom RGB colors

### Texture-Based Alternatives

You **can** use textures for magic circles, but it requires different approaches:

#### Option 1: Armor Stand Method
- Place invisible armor stands with custom items (textured via resource pack)
- **Pros**: Can use any texture, very detailed
- **Cons**: Performance heavy, harder to animate rotation, entity limit concerns

#### Option 2: Falling Block Method
- Use falling blocks with custom textures
- **Pros**: Can display block textures
- **Cons**: Limited to block textures, harder to make circular patterns

#### Option 3: Glow Item Frame Method
- Place glow item frames with custom items
- **Pros**: Can use custom model data textures
- **Cons**: Static (no rotation), requires many entities

#### Option 4: Hybrid Approach (Recommended)
- Keep particle circles for animation and performance
- Add textured armor stands at key positions (center, cardinal points)
- **Pros**: Best of both worlds - animated particles + detailed textures
- **Cons**: More complex implementation

### Recommendation

**Stick with particles** for the following reasons:
1. **Performance**: Particles are much lighter than entities
2. **Animation**: Easy to rotate and animate smoothly
3. **Scalability**: Can have many circles without lag
4. **Visibility**: Always render correctly regardless of angle

If you want more visual detail, consider:
- Adding textured armor stands at the **ritual center** (single entity)
- Using **custom particle textures** via resource pack (advanced)
- Adding **beam textures** to the energy beams (armor stand method)

## Implementation Details

### Magic Circle Drawing
- Circles drawn with 40-90 particles per circle
- Lines drawn with 8-10 particles per segment
- Runes use 5-particle cross patterns
- All patterns rotate smoothly at different speeds

### Performance Optimization
- Particles spawn every 2 ticks (10 times per second)
- Only active rituals render circles
- Automatic cleanup when ritual completes/fails

## Testing

To see different rank complexities:
1. Create a fragment ritual (starts at rank 1)
2. Currently all rituals show rank 1 circles
3. Future: Fragment changer rituals could show player's current rank

## Future Enhancements

Potential improvements:
- Use player's fragment rank for changer rituals
- Add fragment-specific geometric patterns
- Implement texture overlays for high-rank rituals
- Add pulsing effects synchronized with ritual timer
